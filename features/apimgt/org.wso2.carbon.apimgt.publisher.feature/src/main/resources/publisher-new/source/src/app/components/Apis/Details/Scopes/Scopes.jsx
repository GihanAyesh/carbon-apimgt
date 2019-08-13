/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import 'react-tagsinput/react-tagsinput.css';
import PropTypes from 'prop-types';
import React from 'react';
import Api from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import Grid from '@material-ui/core/Grid';
import Card from '@material-ui/core/Card';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';
import withStyles from '@material-ui/core/styles/withStyles';
import { Link } from 'react-router-dom';
import {
    List,
    ListItem,
    ListItemText,
} from '@material-ui/core';
import AddCircle from '@material-ui/icons/AddCircle';
import MUIDataTable from 'mui-datatables';
import Icon from '@material-ui/core/Icon';
import Delete from './Delete';

const styles = theme => ({
    buttonProgress: {
        position: 'relative',
        margin: theme.spacing.unit,
    },
    headline: { paddingTop: theme.spacing.unit * 1.25, paddingLeft: theme.spacing.unit * 2.5 },
    root: {
        width: '100%',
        maxWidth: 800,
        backgroundColor: theme.palette.background.paper,
    },
    heading: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    mainTitle: {
        paddingLeft: 0,
    },
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonIcon: {
        marginRight: 10,
    },
});
/**
 * Generate the scopes UI in API details page.
 * @class Scopes
 * @extends {React.Component}
 */
class Scopes extends React.Component {
    /**
     * Creates an instance of Scopes.
     * @param {any} props Generic props
     * @memberof Scopes
     */
    constructor(props) {
        super(props);
        this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        this.api_data = props.api;
    }

    /**
     * Render Scopes section
     * @returns {React.Component} React Component
     * @memberof Scopes
     */
    render() {
        const { intl, classes, api } = this.props;
        const { scopes } = api;
        const url = `/apis/${api.id}/scopes/create`;
        const editUrl = `/apis/${api.id}/scopes/edit`;
        const columns = [
            intl.formatMessage({
                id: 'Apis.Details.Scopes.Scopes.table.header.name',
                defaultMessage: 'Name',
            }),
            intl.formatMessage({
                id: 'Apis.Details.Scopes.Scopes.table.header.description',
                defaultMessage: 'Description',
            }),
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const roles = value || [];
                            return (
                                roles.join(',')
                            );
                        }
                        return false;
                    },
                    filter: false,
                    label: <FormattedMessage
                        id='Apis.Details.Scopes.Scopes.table.header.roles'
                        defaultMessage='Applying Roles'
                    />,
                },
            },
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            return (
                                <List component='nav' className={classes.root}>
                                    {value.map(resource => (
                                        <ListItem button>
                                            <ListItemText primary={resource} />
                                        </ListItem>
                                    ))}
                                </List>
                            );
                        }
                        return false;
                    },
                    filter: false,
                    label: <FormattedMessage
                        id='Apis.Details.Scopes.Scopes.table.header.usages'
                        defaultMessage='Used In'
                    />,
                },
            },
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const scopeName = tableMeta.rowData[0];
                            return (
                                <table className={classes.actionTable}>
                                    <tr>
                                        <td>
                                            <Link to={{
                                                pathname: editUrl,
                                                state: {
                                                    scopeName,
                                                },
                                            }}
                                            >
                                                <Button>
                                                    <Icon>edit</Icon>
                                                    <FormattedMessage
                                                        id='Apis.Details.Documents.Edit.documents.text.editor.edit'
                                                        defaultMessage='Edit'
                                                    />
                                                </Button>
                                            </Link>
                                        </td>
                                        <td>
                                            <Delete scopeName={scopeName} apiId={this.apiId} api={api} />
                                        </td>
                                    </tr>
                                </table>
                            );
                        }
                        return false;
                    },
                    filter: false,
                    label: <FormattedMessage
                        id='Apis.Details.Scopes.Scopes.table.header.actions'
                        defaultMessage='Actions'
                    />,
                },
            }];
        const options = {
            filterType: 'multiselect',
            selectableRows: false,
        };
        const scopesList = api.scopes.map((scope) => {
            const aScope = [];
            aScope.push(scope.name);
            aScope.push(scope.description);
            aScope.push(scope.bindings.values);
            const resources = api.operations.filter((op) => {
                return op.scopes.includes(scope.name);
            }).map((op) => {
                return op.uritemplate + ' ' + op.httpVerb;
            });
            aScope.push(resources);
            return aScope;
        });

        if (!scopes) {
            return <Progress />;
        }

        if (scopes.length === 0) {
            return (
                <Grid container justify='center'>
                    <Grid item sm={5}>
                        <Card className={classes.card}>
                            <Typography className={classes.headline} gutterBottom variant='h5' component='h2'>
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Scopes.create.scopes.title'
                                    defaultMessage='Create Scopes'
                                />
                            </Typography>
                            <Divider />
                            <CardContent>
                                <Typography align='justify' component='p'>
                                    <FormattedMessage
                                        id='Apis.Details.Scopes.Scopes.scopes.enable.fine.gained.access.control'
                                        defaultMessage={'Scopes enable fine-grained access control to API resources'
                                            + ' based on user roles.'}
                                    />
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Link to={url}>
                                    <Button variant='contained' color='primary' className={classes.button}>
                                        <FormattedMessage
                                            id='Apis.Details.Scopes.Scopes.create.scopes.button'
                                            defaultMessage='Create Scopes'
                                        />
                                    </Button>
                                </Link>
                            </CardActions>
                        </Card>
                    </Grid>
                </Grid>
            );
        }

        return (
            <div className={classes.heading}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage
                            id='Apis.Details.Scopes.Scopes.heading.scope.heading'
                            defaultMessage='Scopes'
                        />
                    </Typography>
                    <Link to={url}>
                        <Button size='small' className={classes.button}>
                            <AddCircle className={classes.buttonIcon} />
                            <FormattedMessage
                                id='Apis.Details.Scopes.Scopes.heading.scope.add_new'
                                defaultMessage='Add New Scope'
                            />
                        </Button>
                    </Link>
                </div>

                <MUIDataTable
                    title={intl.formatMessage({
                        id: 'Apis.Details.Scopes.Scopes.table.scope.name',
                        defaultMessage: 'Scopes',
                    })}
                    data={scopesList}
                    columns={columns}
                    options={options}
                />


            </div>
        );
    }
}

Scopes.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }),
    api: PropTypes.instanceOf(Object).isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

Scopes.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withStyles(styles)(Scopes));
